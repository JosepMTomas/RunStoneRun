#version 300 es

uniform mat4 u_ViewProjectionMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

layout(std140) uniform Matrices
{
	mat4 Model[64];
};

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 vTexCoord;
layout(location = 2) in vec3 vNormal;
layout(location = 3) in vec4 vTangent;
layout(location = 4) in vec3 vColor;

out vec4 v_Position;
out vec2 v_TexCoord;
out vec3 v_Normal;
out vec4 v_Tangent;
out vec3 v_Color;

void main()
{
	//v_Position = u_ModelViewProjectionMatrix * vPosition;
	v_Position = u_ViewProjectionMatrix * Model[gl_InstanceID] * vPosition;
	v_TexCoord = vTexCoord;
	v_Normal = (u_ModelMatrix * vec4(vNormal, 1.0)).xyz;
	v_Normal = normalize(v_Normal);
	v_Tangent = u_ModelMatrix * vTangent;
	
	if(gl_InstanceID == 0)
		v_Color = vec3(1.0, 0.0, 0.0);
	else
		v_Color = vec3(1.0);
	
	gl_Position = v_Position;
}