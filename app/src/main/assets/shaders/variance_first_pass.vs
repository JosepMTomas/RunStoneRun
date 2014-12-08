#version 300 es
  
layout (location=0) in vec3 aPosition;		//object space vertex position

uniform mat4 ModelViewProjection;	//combined modelview projection

//output from the vertex shader
smooth out vec4 clipSpacePos;	//clip space position

void main()
{ 	 
	//get the clipspace vertex position by multiplying the object space vertex 
	//position with the combined modelview project matrix
	gl_Position = ModelViewProjection * vec4(aPosition, 1.0);

	//output the clip space position
	clipSpacePos = gl_Position;
}