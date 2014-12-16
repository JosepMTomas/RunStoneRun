#version 300 es
precision mediump float;

uniform sampler2D colorSampler;
uniform vec3 color;
uniform float time;

in vec2 vTexCoord;

out vec4 fragColor;

void main()
{
	vec4 colorTex = texture(colorSampler, vTexCoord);
	float value = sin(colorTex.x * time);
	
	fragColor = vec4(color * value, value);
}