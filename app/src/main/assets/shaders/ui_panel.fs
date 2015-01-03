#version 300 es

// input(s) from vertex shader
in vec2 vTexCoord;

// uniforms
uniform sampler2D colorSampler;
uniform float opacity;

// shader output(s)
out vec4 fragColor;


void main()
{
	lowp vec4 colorTex = texture(colorSampler, vTexCoord);
	
	fragColor = vec4(colorTex.xyz, colorTex.w * opacity);
}