#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

out vec4 vPosition;
out vec2 vTexCoord;

void main()
{
    vPosition = aPosition;
    vTexCoord = aTexCoord;
	
	gl_Position = vPosition;
}