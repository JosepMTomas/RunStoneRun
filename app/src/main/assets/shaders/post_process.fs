#version 300 es
precision mediump float;

uniform sampler2D colorBuffer;
uniform sampler2D displacementBuffer;

uniform mat4 currentVPMatrix;
uniform mat4 previousVPMatrix;

in vec4 vPosition;
in vec2 vTexCoord;

out vec4 fragColor;

float LinearizeDepth(float z)
{
	float n = 0.1;
	float f = 1000.0;
	return (2.0 * n) / (f + n - z * (f - n));
}

void main()
{
	int iNumSamples = 5;
	float fNumSamples = 5.0;

	//fragColor = texture(colorBuffer, vTexCoord);
	
	// Get the initial color at this pixel.
	vec4 color = texture(colorBuffer, vTexCoord);
	vec4 displacement = (texture(displacementBuffer, vTexCoord));
	displacement = displacement * 2.0 - 1.0;
	
	displacement = vec4(vTexCoord * 2.0 - 1.0, 0.0, 0.0);
	
	float factor = 0.01;
	vec2 texCoord = vTexCoord + (displacement.xy * factor);

	for(int i = 1; i < iNumSamples; ++i)
	{
		// Sample the color buffer along the velocity vector.
		vec4 currentColor = texture(colorBuffer, texCoord);

		// Add the current color to our color sum.
		color += currentColor;
		
		texCoord = texCoord + (displacement.xy * factor);
	}

	// Average all of the samples to get the final blur color.
	//float4 finalColor = color / numSamples;
	fragColor = color / fNumSamples;

}
