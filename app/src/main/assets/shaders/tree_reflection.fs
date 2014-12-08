#version 300 es
precision lowp float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoord;
in float vDistance;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoord);

	if(diffuseTex.w < 0.6) discard;
	
	fragColor = mix(diffuseTex, vec4(1.0), vDistance);
}