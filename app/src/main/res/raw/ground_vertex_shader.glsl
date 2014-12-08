#version 300 es

uniform mat4 u_ModelMatrix;
//uniform mat4 u_ViewProjectionMatrix;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_TexCoord;
layout(location = 2) in vec3 a_Normal;
layout(location = 3) in vec4 a_Tangent;
layout(location = 4) in vec4 a_Color;

uniform mat4 MVP;	// ModelViewProjection matrix
uniform mat4 MV;	// ModelView matrix
uniform mat4 M;		// Model matrix
uniform mat4 N;		// Normal matrix
uniform mat4 S;		// Shadow matrix

out vec4 v_Position;
out vec2 v_TexCoord;
out vec3 v_Normal;
out vec4 v_Tangent;
out vec4 v_Color;

out vec3 vEyeSpaceNormal;
out vec3 vEyeSpacePosition;
out vec4 vShadowCoords;

void main()
{
	//mat4 u_ModelViewProjectionMatrix = u_ViewProjectionMatrix * u_ModelMatrix;

	v_Position = MVP * a_Position;
	v_TexCoord = a_TexCoord;
	v_Normal = vec3(u_ModelMatrix * vec4(a_Normal, 1.0)).xyz;
	v_Normal = a_Normal;
	v_Tangent = u_ModelMatrix * a_Tangent;
	v_Tangent = a_Tangent;
	v_Color = a_Color;
	
	// Shadow mapping
	vEyeSpacePosition = (MV * a_Position).xyz;
	vEyeSpaceNormal = (N * vec4(a_Normal, 1.0)).xyz;
	vShadowCoords = S * (M * a_Position);
	
	gl_Position = v_Position;
}