#version 300 es
precision mediump float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoord;
in vec4 vDiffuse;
in vec4 vAmbient;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	if(diffuseTex.w < 0.6) discard;

	//fragColor = vec4(vNormal.xyz, 1.0);
	lowp vec4 ambient = diffuseTex * vAmbient;
	fragColor = diffuseTex * vDiffuse + ambient;
}