#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

layout (std140) uniform grassProperties
{
	vec4 properties[1024];
};

uniform mat4 viewProjection;

out vec2 vTexCoords;
out float vDistance;

void main()
{
	vec4 current = properties[gl_InstanceID];
	//vec4 position = (aPosition * current.z) + vec4(current.x, 0f, current.y, 0f);
	vec4 position = aPosition;
	position.y *= current.z;
	position += vec4(current.x, 0f, current.y, 0f);

	vTexCoords = aTexCoord;
	vDistance = current.w;
	
	gl_Position = viewProjection * position;
}