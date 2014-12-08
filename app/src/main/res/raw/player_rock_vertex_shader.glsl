#version 300 es

uniform mat4 u_ModelMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

layout(location = 0) in vec4 v_Position;
layout(location = 1) in vec2 v_TexCoord;
layout(location = 2) in vec3 v_Normal;
layout(location = 3) in vec4 v_Tangent;
layout(location = 4) in vec3 v_Color;

out vec4 worldPosition;
out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vTangent;

void main()
{ 
	worldPosition = u_ModelMatrix * v_Position;
	vPosition = u_ModelViewProjectionMatrix * v_Position;
	vTexCoord = v_TexCoord;
	vNormal = (u_ModelMatrix * vec4(v_Normal, 1.0)).xyz;
	vNormal = normalize(vNormal);
	vTangent = u_ModelMatrix * v_Tangent;
	
	gl_Position = vPosition;
}