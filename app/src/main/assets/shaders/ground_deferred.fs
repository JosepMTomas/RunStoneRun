#version 300 es
precision highp float;

layout (location = 0) out float renderTarget0;	// View-Space Normalized Depth
layout (location = 1) out vec4 renderTarget1;	// View-Space Normal (RGB) - Reflection Mask (A)
layout (location = 2) out vec4 renderTarget2;	// Diffuse albedo (RGB) - Emissive Mask (A)
layout (location = 3) out vec4 renderTarget3;	// Eye->Pixel vector (RGB) - ShadowMask (A)

uniform sampler2DShadow uShadowMap;
uniform sampler2D uGrassColorTexture;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec4 vTangent;
in vec3 vColor;

in float vDepth;
in vec3 vEye;

in vec3 displacement;

in vec4 vShadowCoords;

in vec4 viewProjPosition;
in vec4 viewProjPositionD;

void main()
{
	vec4 diffuseAlbedo = texture(uGrassColorTexture, vTexCoord * 2.0);

	//renderTarget0 = vec4(vDepth, 0.0, 0.0, 0.0);
	renderTarget0 = vDepth;
	renderTarget1 = vec4(vNormal, 0.0);
	renderTarget2 = vec4(diffuseAlbedo.xyz * vColor.x, 0.0);
	renderTarget3 = vec4((vEye + 1.0) * 0.5, 0.5);

	/*fragNormals = vec4(vNormal, 1.0);
	fragDiffuse = texture(uGrassColorTexture, vTexCoord * 2.0);
	fragSpecular = vec4(vDepth, 0.0, 0.0, 0.0);
	//fragShadows = vec4(vEye, 0.0);
	//fragShadows = vec4((displacement + 1.0) * 0.5, 0.0);
	fragShadows = vec4((vEye + 1.0) * 0.5, 0.0);*/
	
	/*if(vShadowCoords.w > 1.0)
	{
		float shadow = textureProj(uShadowMap, vShadowCoords);
		//fragColor = mix(vec4(diffuseMix), vec4(diffuseMix)*shadow, 0.75);
		fragShadows = vec4(shadow);
	}
	else
	{
		//fragColor = vec4(diffuseMix);
		fragShadows = vec4(1.0);
	}*/
}
