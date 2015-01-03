#version 300 es

// geometry attribute(s)
layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

// uniforms
uniform mat4 viewProjection;
uniform vec4 positionScale;
uniform vec4 positionTranslation;

// shader output(s)
out vec2 vTexCoord;


void main()
{
	// final position
	vec4 position = (aPosition * positionScale) + positionTranslation;
	
	// final texture coordinates
	vTexCoord = aTexCoord;
	
	// vertex shader output
	gl_Position = viewProjection * position;
}