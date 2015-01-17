#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

layout (std140) uniform particleProperties
{
	//vec4 properties[32];
	mat4 model[32];
};

uniform mat4 viewProjection;

out vec2 vTexCoord;
out float vOpacity;

void main()
{
	////vec4 current = properties[gl_InstanceID];
	//vec4 position = (aPosition * current.z) + vec4(current.x, 0f, current.y, 0f);
	////vec4 position = aPosition;
	////position.xyz *= current.z;
	////position += vec4(current.x, 0.0, current.y, 0.0);
	
	vec4 position = model[gl_InstanceID] * aPosition;

	vTexCoord = aTexCoord;
	vOpacity = 0.75;//current.w;
	
	gl_Position = viewProjection * position;
}