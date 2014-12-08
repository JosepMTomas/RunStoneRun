#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform modelMatrices
{
    mat4 model[128];
};

uniform mat4 worldModel;
uniform mat4 viewProjection;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;

mat4 trunkModel = worldModel * model[gl_InstanceID];
mat4 modelViewProjection = viewProjection * trunkModel;

void main()
{
	vPosition = aPosition;
	vTexCoord = vec2(aTexCoord.x, 1.0 -aTexCoord.y);
	vNormal = aNormal;
	vTangent = aTangent.xyz;
	vBinormal = normalize(cross(vNormal, vTangent)) * aTangent.w;
	
	gl_Position = modelViewProjection * aPosition;
}