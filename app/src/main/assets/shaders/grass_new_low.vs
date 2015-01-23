#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;

layout (std140) uniform grassProperties
{
	vec4 properties[1024];
};

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
	vec4 backColor;
};

uniform mat4 viewProjection;

out vec2 vTexCoords;
out vec4 vDiffuse;
out vec4 vAmbient;
out float vDistance;
out vec4 vBackColor;

void main()
{
	vec4 current = properties[gl_InstanceID];
	//vec4 position = (aPosition * current.z) + vec4(current.x, 0f, current.y, 0f);
	vec4 position = aPosition;
	position.y *= current.z;
	position += vec4(current.x, 0f, current.y, 0f);
	
	float diffuse = dot(vec3(0.0, 1.0, 0.0), vLight);
	vDiffuse = lightColor * diffuse;
	vAmbient = ambientColor;
	vTexCoords = aTexCoord;
	vDistance = current.w;
	vBackColor = backColor;
	
	gl_Position = viewProjection * position;
}