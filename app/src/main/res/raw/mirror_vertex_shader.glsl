#version 300 es

uniform mat4 u_ModelMatrix;
uniform mat4 u_RotationMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

layout (location = 0) in vec4 a_Position;
layout (location = 1) in vec2 a_TexCoord;
layout (location = 2) in vec3 a_Normal;

out vec4 v_Position;
out vec2 v_TexCoord;
out vec3 v_Normal;

void main()
{
	v_Position = u_ModelViewProjectionMatrix * a_Position;
	v_TexCoord = a_TexCoord;
	v_Normal = (u_RotationMatrix * vec4(a_Normal, 1.0)).xyz;
	v_Normal = normalize(v_Normal);
	
	gl_Position = v_Position;
}