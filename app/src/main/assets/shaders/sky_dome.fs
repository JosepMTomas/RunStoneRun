#version 300 es
precision mediump float;

uniform sampler2D gradient1Sampler;
uniform sampler2D gradient2Sampler;
uniform float percent;

//in vec4 vPosition;
in vec2 vTexCoord;
//in vec3 vColor;

out vec4 fragColor;

void main()
{
	lowp vec4 gradient1Color = texture(gradient1Sampler, vTexCoord);
	lowp vec4 gradient2Color = texture(gradient2Sampler, vTexCoord);
	
	fragColor = mix(gradient1Color, gradient2Color, percent);
	//fragColor = vec4(vColor, 1.0);
}