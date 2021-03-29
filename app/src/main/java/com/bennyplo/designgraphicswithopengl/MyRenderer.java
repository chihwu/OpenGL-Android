package com.bennyplo.designgraphicswithopengl;

import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    private final float[] mMVPMatrix = new float[16];//model view projection matrix
    private final float[] mProjectionMatrix = new float[16];//projection mastrix
    private final float[] mViewMatrix = new float[16];//view matrix
    private final float[] mMVMatrix=new float[16];//model view matrix
    private final float[] mModelMatrix=new float[16];//model  matrix
//    private CharacterA mcharA;
//    private CharacterS mcharS;
//    private  Sphere msphere;
//    private ArbitraryShape marbitrarysphere;
    private ArbitraryShapeWithPointLight marbitraryspherewithpointlight;
    private ArbitraryShapeWithDirectionalLight mArbitraryShapeWithDirectionalLight;

    private float mAngle;
    private float mXAngle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//        mcharA=new CharacterA();
//        mcharS = new CharacterS();
//        msphere = new Sphere();
//        marbitrarysphere = new ArbitraryShape();
//        marbitraryspherewithpointlight = new ArbitraryShapeWithPointLight();
        mArbitraryShapeWithDirectionalLight = new ArbitraryShapeWithDirectionalLight();

        mAngle = 0;
        mXAngle = 0;
    }
    public static void checkGlError(String glOperation) {
        int error;
        if ((error = GLES32.glGetError()) != GLES32.GL_NO_ERROR) {
            Log.e("MyRenderer", glOperation + ": glError " + error);
        }
    }
    public static int loadShader(int type, String shaderCode){
        // create a vertex shader  (GLES32.GL_VERTEX_SHADER) or a fragment shader (GLES32.GL_FRAGMENT_SHADER)
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);// add the source code to the shader and compile it
        GLES32.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float left=-ratio,right=ratio;
        Matrix.frustumM(mProjectionMatrix, 0, left,right, -1.0f, 1.0f, 1.0f, 8.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] mRotationMatrix = new float[16];
        float[] mRotationMatrix2 = new float[16];
        // Draw background color
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glClearDepthf(1.0f);//set up the depth buffer
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);//enable depth test (so, it will not look through the surfaces)
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);//indicate what type of depth test
        Matrix.setIdentityM(mMVPMatrix,0);//set the model view projection matrix to an identity matrix
        Matrix.setIdentityM(mMVMatrix,0);//set the model view  matrix to an identity matrix
        Matrix.setIdentityM(mModelMatrix,0);//set the model matrix to an identity matrix
        Matrix.setRotateM(mRotationMatrix2, 0, mAngle, 0f, 1f, 0);//rotate around the y-axis
        Matrix.setRotateM(mRotationMatrix, 0, mXAngle, 1f, 0f, 0);//rotate around the x-axis
//        Matrix.setRotateM(mRotationMatrix2, 0, mAngle, 0f, 0f, 1f);//rotate around the z-axis

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0.0f, 0f, 1.0f,//camera is at (0,0,1)
                0f, 0f, 0f,//looks at the origin
                0f, 1f, 0.0f);//head is down (set to (0,1,0) to look from the top)
        Matrix.translateM(mModelMatrix,0,0.0f,0.0f,-7f);//move backward for 5 units
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix2, 0);
        // Calculate the projection and view transformation
        //calculate the model view matrix
        Matrix.multiplyMM(mMVMatrix,0,mViewMatrix,0,mModelMatrix,0);
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mMVMatrix,0);

//        mcharA.draw(mMVPMatrix);
//        mcharS.draw(mMVPMatrix);
//        msphere.draw(mMVPMatrix);
//        marbitrarysphere.draw(mMVPMatrix);
//        marbitraryspherewithpointlight.draw(mMVPMatrix);
        mArbitraryShapeWithDirectionalLight.draw(mMVPMatrix);
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
    public void setXAngle(float angle) {
        mXAngle = angle;
    }
    public float getAngle() {
        return mAngle;
    }
    public float getXAngle() {
        return mXAngle;
    }
    public void setLightLocation(float px, float py, float pz) {
        if (marbitraryspherewithpointlight != null) {
            marbitraryspherewithpointlight.setLightLocation(px, py, pz);
        }
    }
}
