precision highp float;

varying vec4 v_Position;
varying vec2 v_TexCoord;
varying vec3 v_Normal;
varying vec4 v_Tangent;

void main()
{
	vec3 color = vec3(1.0, 0.0, 0.0);
	vec3 vLight = vec3(0.0, 1.0, 0.0);
	
	float diffuse = dot(v_Normal, vLight);
	diffuse = max(diffuse, 0.0);
	
	vec3 result = color * diffuse;
	
	gl_FragColor = vec4(result, 1.0);
}