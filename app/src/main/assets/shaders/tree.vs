#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform modelMatrices
{
    mat4 model[512];
};

layout (std140) uniform modelIndices
{
	vec4 indices[512];
};

/*layout (std140) uniform worldModelMatrices
{
	mat4 worldModel[9];
};*/

layout (std140) uniform worldMVPMatrices
{
	mat4 worldMVP[9];
};

uniform sampler2DShadow shadowMapSampler;
uniform mat4 shadowMatrix;

uniform mat4 viewProjection;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;

//flat out int index;

//out float vShadows;
out float vDiffuse;
out lowp float vDistance;

const vec3 vLight = normalize(vec3(1.0, 0.0, 0.0));

int worldModelIndex = int(indices[gl_InstanceID].x);
//mat4 treeModel = model[gl_InstanceID];
//mat4 mvp = viewProjection * worldModel[worldModelIndex];
//mat4 modelViewProjection = mvp * treeModel;
//mat4 treeModel = worldModel[worldModelIndex] * model[gl_InstanceID];
//mat4 modelViewProjection = viewProjection * treeModel;

mat4 treeModel = model[gl_InstanceID];
mat4 modelViewProjection = worldMVP[worldModelIndex] * treeModel;

void main()
{
	//index = worldModelIndex;

	vPosition = aPosition;
	vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
	vNormal = normalize((treeModel * vec4(aNormal, 0.0)).xyz);
	vTangent = aTangent.xyz;
	vBinormal = normalize(cross(vNormal, vTangent)) * aTangent.w;
	
	/*vec4 shadowCoords = shadowMatrix * (treeModel * aPosition);
	vShadows = textureProj(shadowMapSampler, shadowCoords);
	vShadows += 0.25;
	vShadows = clamp(vShadows, 0.0, 1.0);*/
	
	vDiffuse = dot(vNormal, vLight);
	vDiffuse *= 2.0;
	vDiffuse = clamp(vDiffuse, 0.0, 1.0);
	//vDiffuse += 0.25;
	
	/*vDistance = abs(distance(treeModel * aPosition, vec4(0.0))) * 0.00125;
	vDistance = clamp(vDistance, 0.0, 1.0);
	vDistance = vDistance * vDistance;*/
	vDistance = indices[gl_InstanceID].y;
	
	gl_Position = modelViewProjection * aPosition;
}