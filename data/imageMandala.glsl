uniform sampler2D img;
uniform vec2 resolution;
uniform float time;

#define pi 3.14159

float random(float x){
    return fract(sin(x*35.12145)*345.4861);
}

vec2 random2(float x){
    return vec2(random(x-323.151), random(x+412.121));
}

vec2 wrapAround(vec2 p){
    vec2 x = vec2(1);
    return mod(p,x) - x*0.5;
}

float sdBox( in vec2 p, in vec2 size ){
    vec2 d = abs(p)-size;
    return length(max(d,vec2(0))) + min(max(d.x,d.y),0.0);
}


vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
float permute(float x){ return floor(mod(((x*34.0)+1.0)*x, 289.0)); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }
float taylorInvSqrt(float r){ return 1.79284291400159 - 0.85373472095314 * r; }
vec4 grad4(float j, vec4 ip){
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p, s;

    p.xyz = floor(fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    s = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (s.xyz*2.0 - 1.0) * s.www;

    return p;
}
float snoise(vec4 v){
    const vec2  C = vec2(0.138196601125010504, // (5 - sqrt(5))/20  G4
    0.309016994374947451);// (sqrt(5) - 1)/4   F4
    // First corner
    vec4 i  = floor(v + dot(v, C.yyyy));
    vec4 x0 = v -   i + dot(i, C.xxxx);

    // Other corners

    // Rank sorting originally contributed by Bill Licea-Kane, AMD (formerly ATI)
    vec4 i0;

    vec3 isX = step(x0.yzw, x0.xxx);
    vec3 isYZ = step(x0.zww, x0.yyz);
    //  i0.x = dot( isX, vec3( 1.0 ) );
    i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;

    //  i0.y += dot( isYZ.xy, vec2( 1.0 ) );
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;

    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;

    // i0 now contains the unique values 0,1,2,3 in each channel
    vec4 i3 = clamp(i0, 0.0, 1.0);
    vec4 i2 = clamp(i0-1.0, 0.0, 1.0);
    vec4 i1 = clamp(i0-2.0, 0.0, 1.0);

    //  x0 = x0 - 0.0 + 0.0 * C
    vec4 x1 = x0 - i1 + 1.0 * C.xxxx;
    vec4 x2 = x0 - i2 + 2.0 * C.xxxx;
    vec4 x3 = x0 - i3 + 3.0 * C.xxxx;
    vec4 x4 = x0 - 1.0 + 4.0 * C.xxxx;

    // Permutations
    i = mod(i, 289.0);
    float j0 = permute(permute(permute(permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute(permute(permute(permute (
    i.w + vec4(i1.w, i2.w, i3.w, 1.0))
    + i.z + vec4(i1.z, i2.z, i3.z, 1.0))
    + i.y + vec4(i1.y, i2.y, i3.y, 1.0))
    + i.x + vec4(i1.x, i2.x, i3.x, 1.0));
    // Gradients
    // ( 7*7*6 points uniformly over a cube, mapped onto a 4-octahedron.)
    // 7*7*6 = 294, which is close to the ring size 17*17 = 289.

    vec4 ip = vec4(1.0/294.0, 1.0/49.0, 1.0/7.0, 0.0);

    vec4 p0 = grad4(j0, ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);

    // Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4, p4));

    // Mix contributions from the five corners
    vec3 m0 = max(0.6 - vec3(dot(x0, x0), dot(x1, x1), dot(x2, x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3, x3), dot(x4, x4)), 0.0);
    m0 = m0 * m0;
    m1 = m1 * m1;
    return 49.0 * (dot(m0*m0, vec3(dot(p0, x0), dot(p1, x1), dot(p2, x2)))
    + dot(m1*m1, vec2(dot(p3, x3), dot(p4, x4))));
}


float fbm (vec4 p) {
    float sum = 0.;
    float amp = 1;
    float freq = 1;
    // Loop of octaves
    for (int i = 0; i < 5; i++) {
        sum += amp*snoise(p*freq);
        freq *= 2.;
        amp *= .5;
        p += vec4(3.123, 2.456, 1.121, 2.4545);
    }
    return sum;
}

mat2 rotate2d(float angle){
    return mat2(cos(angle),-sin(angle), sin(angle),cos(angle));
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    uv.y = 1.-uv.y;
    float scl = 800.;
    vec2 id = (floor(uv*scl)+.5)*(1./scl);
    float tr = .02;
    float a = atan(id.y-.5, id.x-.5);
    float d = length(id-.5);
    vec2 t = vec2(tr*cos(time), tr*sin(time));
    float n = pi*fbm(vec4(d*1.8, .02*cos(a*14.), t));
    float nr = .9;
    vec2 imgCoord =  vec2(.0, .2+.2*sin(t)) + vec2(nr*cos(n), nr*sin(n));
    vec3 color = texture2D(img,imgCoord).rgb;
    color = color*smoothstep(.8, .5, d);
    gl_FragColor = vec4(color, 1.);
}
