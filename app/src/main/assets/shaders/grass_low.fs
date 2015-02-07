#version 300 es
precision lowp float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoords;
in vec4 vDiffuse;
in vec4 vAmbient;
in float vDistance;
in vec4 vBackColor;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoords);
	
	if(diffuseTex.w < 0.25) discard;
	
	fragColor = mix((diffuseTex * vDiffuse) + (diffuseTex * vAmbient), vBackColor, vDistance);
}