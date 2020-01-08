uniform sampler2D texture;
uniform vec2 resolution;

vec3 rgb( in vec3 c ){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 tex = texture(texture, uv).rgb;
    float pct = smoothstep(0., 0.35, tex.b);
//    float pct = tex.b;
    vec3 clr = rgb(vec3(.57, 1-pct*.5, pct));
    clr -= smoothstep(0, 1.2, length(cv));
    gl_FragColor = vec4(clr, 1.);
}