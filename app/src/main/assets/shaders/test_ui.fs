#version 300 es
precision mediump float;

in vec2 vTexCoord;

uniform sampler2D numbersSampler;

out vec4 fragColor;


void main()
{
	// texture
	lowp vec4 numbersTex = texture(numbersSampler, vTexCoord);
	
	fragColor = numbersTex;
}