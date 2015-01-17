#version 300 es
precision mediump float;

uniform sampler2D diffuseSampler;

in vec2 vTexCoord;
in float vOpacity;

out vec4 fragColor;

void main()
{
	vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	diffuseTex.w *= vOpacity;
	
	fragColor = diffuseTex;
	//fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}