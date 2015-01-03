#version 300 es
precision highp float;

uniform sampler2D colorSampler;
uniform float speedFactor;

in vec2 vTexCoord;

//layout (location = 0) out vec4 colorBuffer;
out vec4 fragColor;

vec2 pow2(vec2 value, float exponent)
{
	return vec2(pow(value.x, exponent), pow(value.y, exponent));
}

void main()
{
	lowp vec4 colorTex = texture(colorSampler, vTexCoord);
	//fragColor = colorTex;
	
	vec2 screenCoord = vTexCoord * 2.0 - 1.0;
	//vec2 screenSpeed = pow2(screenCoord, 2.0);
	//screenSpeed = screenSpeed * screenCoord;
	float factor = 0.008 * speedFactor; //0.003
	
	lowp vec4 color = vec4(0.0);
	vec2 screenFactor = screenCoord * factor;
	//vec2 texCoord = vTexCoord + (screenSpeed * factor);
	vec2 texCoord = vTexCoord + screenFactor;

	for(int i = 1; i < 5; ++i)
	{
		// Sample the color buffer along the velocity vector.
		lowp vec4 currentColor = texture(colorSampler, texCoord);

		// Add the current color to our color sum.
		color += currentColor;
		
		//texCoord = texCoord + (screenSpeed * factor);
		texCoord = texCoord + screenFactor;
	}
	
	//fragColor = colorTex;
	//fragColor = vec4(vTexCoord, 0.0, 1.0);
	//fragColor = vec4(abs(screenCoord), 0.0, 1.0);
	fragColor = color / 5.0;
	//fragColor = texture(colorSampler, vTexCoord);
	
}
