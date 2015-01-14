#version 300 es
precision mediump float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoords;
in float vDistance;
in float vShadows;
in vec4 vLightColor;
in vec4 vAmbient;
in vec4 vBackColor;
//in vec4 vLodColor;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoords);
	
	if(diffuseTex.w < 0.25) discard;
	
	vec4 ambientTex = diffuseTex * vAmbient;
	
	diffuseTex *= vLightColor;
	
	fragColor = mix(diffuseTex * vShadows + ambientTex, vBackColor, vDistance);
	//fragColor = vLodColor;
}