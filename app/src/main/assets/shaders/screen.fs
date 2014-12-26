#version 300 es
precision highp float;

uniform sampler2D colorSampler;

in vec2 vTexCoord;

out vec4 fragColor;


void main()
{
	lowp vec4 colorTex = texture(colorSampler, vTexCoord);
	fragColor = colorTex;
}
