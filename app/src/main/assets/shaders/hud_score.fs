#version 300 es

// input(s) from vertex shader
in vec2 vTexCoord;

// uniforms
uniform sampler2D numbersSampler;
uniform vec3 color;
uniform float opacity;

// shader output(s)
out vec4 fragColor;


void main()
{
	lowp vec4 numbersTex = texture(numbersSampler, vTexCoord);
	
	//vec3 texColor = numbersTex.xyz * color;
	
	fragColor = vec4(color, numbersTex.w * opacity);
	//fragColor = numbersTex;
	//fragColor = vec4(1.0);
	//fragColor = vec4(vTexCoord.x, vTexCoord.y, 0.0, 1.0);
}