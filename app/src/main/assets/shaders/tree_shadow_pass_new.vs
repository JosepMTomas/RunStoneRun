#version 300 es

layout (location = 0) in vec4 aPosition;

layout (std140) uniform treeProperties
{
	vec4 properties[512];
};

uniform mat4 viewProjection;

void main()
{
	vec3 position = aPosition.xyz;
	vec4 property = properties[gl_InstanceID];
	position *= property.z;
	position += vec3(property.x, 0.0, property.y);

	gl_Position = viewProjection * vec4(position, 1.0);
}