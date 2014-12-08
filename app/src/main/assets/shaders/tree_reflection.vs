#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

layout (std140) uniform treeProperties
{
	vec4 properties[512];
};

uniform mat4 viewProjection;

out vec2 vTexCoord;
out float vDistance;

void main()
{
	vTexCoord = aTexCoord;
	
	vec4 property = properties[gl_InstanceID];
	vec3 position = (aPosition.xyz * property.z);
	position += vec3(property.x, 0.0, property.y);
	
	vDistance = property.w * property.w;
	
	gl_Position = viewProjection * vec4(position, 1.0);
}