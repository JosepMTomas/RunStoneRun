#version 300 es

layout (location = 0) in vec4 aPosition;

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

uniform mat4 viewProjection;

int worldModelIndex = int(indices[gl_InstanceID].x);
/*mat4 treeModel = worldModel[worldModelIndex] * model[gl_InstanceID];
mat4 modelViewProjection = viewProjection * treeModel;*/
mat4 modelViewProjection = worldMVP[worldModelIndex] * model[gl_InstanceID];

void main()
{
	gl_Position = modelViewProjection * aPosition;
}