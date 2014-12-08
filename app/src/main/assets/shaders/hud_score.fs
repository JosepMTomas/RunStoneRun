#version 300 es

// input(s) from vertex shader
in vec2 vTexCoord;

// uniforms
uniform sampler2D numbersSampler;

// shader output(s)
out vec4 fragColor;


void main()
{
	lowp vec4 numbersTex = texture(numbersSampler, vTexCoord);
	
	fragColor = vec4(vec3(numbersTex.x + 0.25), numbersTex.x);
	//fragColor = numbersTex;
	//fragColor = vec4(1.0);
	//fragColor = vec4(vTexCoord.x, vTexCoord.y, 0.0, 1.0);
}