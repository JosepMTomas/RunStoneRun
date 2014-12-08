#version 300 es

// attributes
layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

// uniforms
uniform mat4 viewProjection;
uniform vec4 positionOffset;
uniform vec2 texCoordOffset;

// outputs
out vec2 vTexCoord;

void main()
{
	// final position
	vec4 position = aPosition + positionOffset;
	
	// final texCoord
	vTexCoord = aTexCoord + texCoordOffset;
	
	// position output
	gl_Position = viewProjection * position;
}