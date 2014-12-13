#version 300 es

// geometry attribute(s)
layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in float aGradient;

// uniforms
uniform mat4 viewProjection;
uniform vec4 positionOffset;
uniform vec2 texCoordOffset;

// shader output(s)
out vec2 vTexCoord;
out float vGradient;


void main()
{
	// final position
	vec4 position = aPosition + positionOffset;
	
	// final texture coordinates
	vTexCoord = aTexCoord;
	
	// gradient
	vGradient = aGradient;
	
	// vertex shader output
	gl_Position = viewProjection * position;
}