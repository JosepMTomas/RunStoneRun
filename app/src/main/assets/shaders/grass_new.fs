#version 300 es
precision mediump float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoords;
in float vDistance;
in float vShadows;
in vec4 ambient;
//in vec4 vLodColor;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoords);
	
	if(diffuseTex.w < 0.25) discard;
	
	vec4 ambientTex = diffuseTex * ambient;
	
	fragColor = mix(diffuseTex * vShadows + ambientTex, vec4(1.0), vDistance);
	//fragColor = vLodColor;
}