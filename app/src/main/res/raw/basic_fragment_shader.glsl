#version 300 es
precision highp float;

in vec4 v_Position;
in vec2 v_TexCoord;
in vec3 v_Normal;
in vec4 v_Tangent;
in vec3 v_Color;

out vec4 fragColor;

void main()
{
	vec3 vLight = vec3(0.0, 1.0, 0.0);
	
	float diffuse = dot(v_Normal, vLight);
	diffuse = max(diffuse, 0.0);
	
	vec3 result = v_Color * diffuse;
	
	fragColor = vec4(result, 1.0);
	//gl_FragColor = vec4(result, 1.0);
}