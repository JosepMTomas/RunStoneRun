#version 300 es
  
layout (location = 0) in vec4 aPosition;

layout (std140) uniform modelMatrices
{
    mat4 model[128];
};

uniform mat4 worldModel;
uniform mat4 viewProjection;

mat4 model2 = worldModel * model[gl_InstanceID];
mat4 modelViewProjection = viewProjection * model2;

void main()
{ 	 
	//get the clipspace vertex position by multiplying the object space vertex 
	//position with the combined modelview project matrix
	gl_Position = modelViewProjection * aPosition;
}