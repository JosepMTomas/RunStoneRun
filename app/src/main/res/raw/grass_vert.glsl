#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout(std140) uniform modelMatrices
{
    mat4 model[128];
};

uniform mat4 viewProjection;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;

void main()
{
	mat4 modelViewProjection = viewProjection * modelMatrices.model[gl_InstanceID];
	
	vPosition = modelViewProjection * aPosition;
	vTexCoord = aTexCoord;
	vNormal = (modelViewProjection * vec4(aNormal, 0.0)).xyz;
	vTangent = (modelViewProjection * vec4(aTangent.xyz, 0.0)).xyz;
	vBinormal = cross(aNormal, aTangent.xyz) * aTangent.w;

	gl_Position = vPosition;
}