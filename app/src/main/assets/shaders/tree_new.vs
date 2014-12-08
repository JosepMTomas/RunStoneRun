#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform treeProperties
{
    vec4 properties[512];
};

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

uniform mat4 viewProjection;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vAmbient;


out float vDiffuse;
out lowp float vDistance;

//const vec3 vLight = vec3(0.71, 0.71, 0.0);

void main()
{
	vec4 property = properties[gl_InstanceID];

	// Final position
	/*vPosition = aPosition;
	vPosition *= vec4(property.z,property.z,property.z,1.0);
	vPosition += vec4(property.x, 0.0, property.y, 0.0);*/
	vec3 position = aPosition.xyz * property.z;
	position += vec3(property.x, 0.0, property.y);
	vPosition = vec4(position, 1.0);
	
	// Texture coordinates & normal attributes
	vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
	vNormal = aNormal;
	
	// Per-vertex diffuse component
	vDiffuse = dot(vNormal, vLight);
	//vDiffuse *= 2.0;
	vDiffuse = clamp(vDiffuse, 0.0, 1.0);
	
	vAmbient = ambientColor;

	// Vertex distance to origin
	vDistance = property.w;
	vDistance *= vDistance;
	
	gl_Position = viewProjection * vPosition;
}