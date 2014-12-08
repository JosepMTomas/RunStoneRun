#version 300 es
  
layout(location=0) in vec4 vVertex; //object space vertex position

uniform mat4 MVP;	//combined modelview projection

void main()
{ 	 
	//get the clipspace vertex position by multiplying the object space vertex 
	//position with the combined modelview project matrix
	gl_Position = MVP * vVertex;
}