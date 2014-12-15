#version 300 es

// input(s) from vertex shader
in vec2 vTexCoord;
in float vGradient;

// uniforms
uniform sampler2D progressSampler;
uniform float percent;

// shader output(s)
out vec4 fragColor;


void main()
{
	lowp vec4 progressTex = texture(progressSampler, vTexCoord);
	
	float value = vGradient + percent;
	//value = clamp(value, 0.0, 1.0);
	value = floor(value);
	value += 0.5;
	
	fragColor = vec4(vec3(value), value * progressTex.w);
	
	//fragColor = vec4(vec3(numbersTex.x + 0.25), numbersTex.x);
	//fragColor = numbersTex;
	//fragColor = vec4(1.0);
	//fragColor = vec4(vTexCoord.x, vTexCoord.y, 0.0, 1.0);
}