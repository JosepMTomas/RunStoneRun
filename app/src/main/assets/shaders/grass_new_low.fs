#version 300 es
precision lowp float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoords;
in float vDistance;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoords);
	
	if(diffuseTex.w < 0.25) discard;
	
	fragColor = mix(diffuseTex, vec4(1.0), vDistance);
}