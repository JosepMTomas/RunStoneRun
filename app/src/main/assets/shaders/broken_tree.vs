#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

uniform mat4 model;
uniform mat4 modelViewProjection;

out vec2 vTexCoord;
out vec4 vDiffuse;
out vec4 vAmbient;

void main()
{
	vTexCoord = aTexCoord;
	vec3 normal = (model * vec4(aNormal, 0.0)).xyz;
	float diffuse = dot(normal, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);
	vDiffuse = diffuse * lightColor;
	vAmbient = ambientColor;
	
	gl_Position = modelViewProjection * aPosition;
}