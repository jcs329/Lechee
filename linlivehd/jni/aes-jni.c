/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>


#include <openssl/evp.h>
#include <openssl/aes.h>

#define nr_bits 2048
#define STP_MAX_STR_RES 1024
#define STP_STR_L 512

#define RvInt int
#define IN
#define INOUT
#define RvChar char
#define RvUint8 unsigned short

static char encoding_table[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
    'w', 'x', 'y', 'z', '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', '+', '/'};

static const RvInt decTable[] =
{
    /*  0  */ -2,
    /*  1  */ -2,
    /*  2  */ -2,
    /*  3  */ -2,
    /*  4  */ -2,
    /*  5  */ -2,
    /*  6  */ -2,
    /*  7  */ -2,
    /*  8  */ -2,
    /*  9  */ -2,
    /* 10  */ -2,
    /* 11  */ -2,
    /* 12  */ -2,
    /* 13  */ -2,
    /* 14  */ -2,
    /* 15  */ -2,
    /* 16  */ -2,
    /* 17  */ -2,
    /* 18  */ -2,
    /* 19  */ -2,
    /* 20  */ -2,
    /* 21  */ -2,
    /* 22  */ -2,
    /* 23  */ -2,
    /* 24  */ -2,
    /* 25  */ -2,
    /* 26  */ -2,
    /* 27  */ -2,
    /* 28  */ -2,
    /* 29  */ -2,
    /* 30  */ -2,
    /* 31  */ -2,
    /* 32  */ -2,
    /* 33 !*/ -2,
    /* 34 "*/ -2,
    /* 35 #*/ -2,
    /* 36 $*/ -2,
    /* 37 %*/ -2,
    /* 38 &*/ -2,
    /* 39 '*/ -2,
    /* 40 (*/ -2,
    /* 41 )*/ -2,
    /* 42 **/ -2,
    /* 43 +*/ 62,
    /* 44 ,*/ -2,
    /* 45 -*/ -2,
    /* 46 .*/ -2,
    /* 47 / */ 63,
    /* 48 0*/ 52,
    /* 49 1*/ 53,
    /* 50 2*/ 54,
    /* 51 3*/ 55,
    /* 52 4*/ 56,
    /* 53 5*/ 57,
    /* 54 6*/ 58,
    /* 55 7*/ 59,
    /* 56 8*/ 60,
    /* 57 9*/ 61,
    /* 58 :*/ -2,
    /* 59 ;*/ -2,
    /* 60 <*/ -2,
    /* 61 =*/ -1,
    /* 62 >*/ -2,
    /* 63 ?*/ -2,
    /* 64 @*/ -2,
    /* 65 A*/  0,
    /* 66 B*/  1,
    /* 67 C*/  2,
    /* 68 D*/  3,
    /* 69 E*/  4,
    /* 70 F*/  5,
    /* 71 G*/  6,
    /* 72 H*/  7,
    /* 73 I*/  8,
    /* 74 J*/  9,
    /* 75 K*/ 10,
    /* 76 L*/ 11,
    /* 77 M*/ 12,
    /* 78 N*/ 13,
    /* 79 O*/ 14,
    /* 80 P*/ 15,
    /* 81 Q*/ 16,
    /* 82 R*/ 17,
    /* 83 S*/ 18,
    /* 84 T*/ 19,
    /* 85 U*/ 20,
    /* 86 V*/ 21,
    /* 87 W*/ 22,
    /* 88 X*/ 23,
    /* 89 Y*/ 24,
    /* 90 Z*/ 25,
    /* 91 [*/ -2,
    /* 92 \*/ -2,
    /* 93 ]*/ -2,
    /* 94 ^*/ -2,
    /* 95 _*/ -2,
    /* 96 `*/ -2,
    /* 97 a*/ 26,
    /* 98 b*/ 27,
    /* 99 c*/ 28,
    /*100 d*/ 29,
    /*101 e*/ 30,
    /*102 f*/ 31,
    /*103 g*/ 32,
    /*104 h*/ 33,
    /*105 i*/ 34,
    /*106 j*/ 35,
    /*107 k*/ 36,
    /*108 l*/ 37,
    /*109 m*/ 38,
    /*110 n*/ 39,
    /*111 o*/ 40,
    /*112 p*/ 41,
    /*113 q*/ 42,
    /*114 r*/ 43,
    /*115 s*/ 44,
    /*116 t*/ 45,
    /*117 u*/ 46,
    /*118 v*/ 47,
    /*119 w*/ 48,
    /*120 x*/ 49,
    /*121 y*/ 50,
    /*122 z*/ 51,
    /*123 {*/ -2,
    /*124 |*/ -2,
    /*125 }*/ -2,
    /*126 ~*/ -2,
    /*127  */ -2
};

static char *decoding_table = NULL;
static int mod_table[] = {0, 2, 1};

static int encryptLEN=0;
static int decryptLEN=0;

EVP_CIPHER_CTX e_ctx;
EVP_CIPHER_CTX d_ctx;
unsigned char *qkey = "peacec(k)ooking";
unsigned int   salt[] = {12345,54321};

char enUSER[256];

char *base64_encode(const unsigned char *data,
	size_t input_length,
	size_t *output_length) {

    int i = 0, j = 0;
    *output_length = 4 * ((input_length + 2) / 3);

    char *encoded_data = malloc(*output_length);
    if (encoded_data == NULL) return NULL;

    for (i = 0, j = 0; i < input_length;) {

	uint32_t octet_a = i < input_length ? data[i++] : 0;
	uint32_t octet_b = i < input_length ? data[i++] : 0;
	uint32_t octet_c = i < input_length ? data[i++] : 0;

	uint32_t triple = (octet_a << 0x10) + (octet_b << 0x08) + octet_c;

	encoded_data[j++] = encoding_table[(triple >> 3 * 6) & 0x3F];
	encoded_data[j++] = encoding_table[(triple >> 2 * 6) & 0x3F];
	encoded_data[j++] = encoding_table[(triple >> 1 * 6) & 0x3F];
	encoded_data[j++] = encoding_table[(triple >> 0 * 6) & 0x3F];
    }

    for (i = 0; i < mod_table[input_length % 3]; i++)
	encoded_data[*output_length - 1 - i] = '=';

    return encoded_data;
}


unsigned char *base64_decode(const char *data,
	size_t input_length,
	size_t *output_length) {

    int i = 0, j = 0;
    if (decoding_table == NULL) build_decoding_table();

    if (input_length % 4 != 0) return NULL;

    *output_length = input_length / 4 * 3;
    if (data[input_length - 1] == '=') (*output_length)--;
    if (data[input_length - 2] == '=') (*output_length)--;

    unsigned char *decoded_data = malloc(*output_length);
    if (decoded_data == NULL) return NULL;

    for (i = 0, j = 0; i < input_length;) {

	uint32_t sextet_a = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
	uint32_t sextet_b = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
	uint32_t sextet_c = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
	uint32_t sextet_d = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];

	uint32_t triple = (sextet_a << 3 * 6)
	    + (sextet_b << 2 * 6)
	    + (sextet_c << 1 * 6)
	    + (sextet_d << 0 * 6);

	if (j < *output_length) decoded_data[j++] = (triple >> 2 * 8) & 0xFF;
	if (j < *output_length) decoded_data[j++] = (triple >> 1 * 8) & 0xFF;
	if (j < *output_length) decoded_data[j++] = (triple >> 0 * 8) & 0xFF;
    }

    return decoded_data;
}


void build_decoding_table() {

    decoding_table = malloc(256);
    int i = 0;

    for (i = 0; i < 64; i++)
	decoding_table[(unsigned char) encoding_table[i]] = i;
}


void base64_cleanup() {
    free(decoding_table);
}


int aes_init(unsigned char *key_data, int key_data_len, unsigned char *salt, EVP_CIPHER_CTX *e_ctx,
	EVP_CIPHER_CTX *d_ctx)
{
    int i, nrounds = 1;
    unsigned char key[32], iv[32];

    /*
     * Gen key & IV for AES 256 CBC mode. A SHA1 digest is used to hash the supplied key material.
     * nrounds is the number of times the we hash the material. More rounds are more secure but
     * slower.
     */
    i = EVP_BytesToKey(EVP_aes_256_cbc(), EVP_md5(), NULL, key_data, key_data_len, nrounds, key, iv);
    if (i != 32) {
	printf("Key size is %d bits - should be 256 bits\n", i);
	return -1;
    }


//   printf("AES key+iv=%s %s\n",key,iv);
//    for (i=0;i<16;i++) printf("%02X",iv[i]);
//    printf("\n");
    EVP_CIPHER_CTX_init(e_ctx);
    EVP_EncryptInit_ex(e_ctx, EVP_aes_256_cbc(), NULL, key, iv);
    EVP_CIPHER_CTX_init(d_ctx);
    EVP_DecryptInit_ex(d_ctx, EVP_aes_256_cbc(), NULL, key, iv);

    return 0;
}

unsigned char *aes_encrypt(EVP_CIPHER_CTX *e, unsigned char *plaintext, int *len)
{
    /* max ciphertext len for a n bytes of plaintext is n + AES_BLOCK_SIZE -1 bytes */
    int c_len = *len + AES_BLOCK_SIZE, f_len = 0;
    unsigned char *ciphertext = malloc(c_len);

    /* allows reusing of 'e' for multiple encryption cycles */
    EVP_EncryptInit_ex(e, NULL, NULL, NULL, NULL);

    /* update ciphertext, c_len is filled with the length of ciphertext generated,
     *len is the size of plaintext in bytes */
    EVP_EncryptUpdate(e, ciphertext, &c_len, plaintext, *len);

    /* update ciphertext with the final remaining bytes */
    EVP_EncryptFinal_ex(e, ciphertext+c_len, &f_len);

    *len = c_len + f_len;
    return ciphertext;
}

unsigned char *aes_decrypt(EVP_CIPHER_CTX *e, unsigned char *ciphertext, int *len)
{
    /* because we have padding ON, we must allocate an extra cipher block size of memory */
    int p_len = *len, f_len = 0;
    unsigned char *plaintext = malloc(p_len + AES_BLOCK_SIZE);

    EVP_DecryptInit_ex(e, NULL, NULL, NULL, NULL);
    EVP_DecryptUpdate(e, plaintext, &p_len, ciphertext, *len);
    EVP_DecryptFinal_ex(e, plaintext+p_len, &f_len);

    *len = p_len + f_len;
    return plaintext;
}

char* decrypt(char * data, int str_len)
{
    /* "opaque" encryption, decryption ctx structures that libcrypto uses to record
       status of enc/dec operations */
    EVP_CIPHER_CTX en, de;

    char *qkey="peacec(k)ooking";

    /* 8 bytes to salt the key_data during key generation. This is an example of
       compiled in salt. We just read the bit pattern created by these two 4 byte
       integers on the stack as 64 bits of contigous salt material -
       ofcourse this only works if sizeof(int) >= 4 */
    unsigned int salt[] = {12345, 54321};
    unsigned char *key_data;
    int key_data_len, i;


    /* the key_data is read from the argument list */
    key_data = qkey;
    key_data_len = strlen(qkey);

    /* gen key and iv. init the cipher ctx object */
    if (aes_init(key_data, key_data_len, (unsigned char *)&salt, &en, &de)) {
	printf("Couldn't initialize AES cipher\n");
	return -1;
    }

    /* encrypt and decrypt each input string and compare with the original */

    char *plaintext;
    char *plaintext1;
    unsigned char *ciphertext;
    int olen, len;

    plaintext = (char *)aes_decrypt(&de, data, &str_len);
    decryptLEN = str_len;


    EVP_CIPHER_CTX_cleanup(&en);
    EVP_CIPHER_CTX_cleanup(&de);

    return plaintext;
}

char* encrypt(char * data, int encrypt_len)
{
    /* "opaque" encryption, decryption ctx structures that libcrypto uses to record
       status of enc/dec operations */
    EVP_CIPHER_CTX en, de;

    char *qkey="peacec(k)ooking";

    /* 8 bytes to salt the key_data during key generation. This is an example of
       compiled in salt. We just read the bit pattern created by these two 4 byte
       integers on the stack as 64 bits of contigous salt material -
       ofcourse this only works if sizeof(int) >= 4 */
    unsigned int salt[] = {12345, 54321};
    unsigned char *key_data;
    int key_data_len, i;


    /* the key_data is read from the argument list */
    key_data = qkey;
    key_data_len = strlen(qkey);

    /* gen key and iv. init the cipher ctx object */
    if (aes_init(key_data, key_data_len, (unsigned char *)&salt, &en, &de)) {
	printf("Couldn't initialize AES cipher\n");
	return -1;
    }



    /* encrypt and decrypt each input string and compare with the original */

    char *plaintext;
    unsigned char *ciphertext;
    int olen, len;

    encrypt_len = strlen(data)+1;

    ciphertext = aes_encrypt(&en, (unsigned char *)data, &encrypt_len);
    encryptLEN=encrypt_len;
    EVP_CIPHER_CTX_cleanup(&en);
    EVP_CIPHER_CTX_cleanup(&de);

    return ciphertext;
}
/*
char* jstringTostring(JNIEnv* env, jstring jstr)
{
  char* rtn = NULL;
  jclass clsstring = env->FindClass("java/lang/String");
  jstring strencode = env->NewStringUTF("utf-8");
  jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
  jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
  jsize alen = env->GetArrayLength(barr);
  jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
  if (alen > 0)
  {
    rtn = (char*)malloc(alen + 1);
    memcpy(rtn, ba, alen);
    rtn[alen] = 0;
  }
  env->ReleaseByteArrayElements(barr, ba, 0);
  return rtn;
}/**/



RvInt rvDecodeB64(
    IN const unsigned char* inTxt,
    IN RvInt inLen,
    INOUT unsigned char* outTxt,
    IN RvInt outLen)
{
    unsigned char in[4], *out;
    RvChar c;
    RvInt i, testLen = outLen;

    out = outTxt;
    outLen = 0;

    while (inLen)
    {
        for (i=0; i<4 && inLen;  i++, inTxt++, inLen--)
        {
            c = (RvChar) decTable[*inTxt];
            if (c == (RvChar)-2)
                return -1;
            if (c == (RvChar)-1)
                break;
            in[i] = c;
        }

        if (testLen < (i-1))
            /* not enough room for output */
            return -1;
        testLen -= (i-1);

        *out++ = (RvUint8) (in[0] << 2 | in[1] >> 4);
        if (i > 2)
        {
            *out++ = (RvUint8) (in[1] << 4 | in[2] >> 2);
            if (i == 4)
                *out++ = (RvUint8) (((in[2] << 6) & 0xc0) | in[3]);
        }

        outLen += i-1;

        if (i < 4)
            break;
    }

    return outLen;
}


int test_openssl()
{
  //RSA *rsa = RSA_generate_key(nr_bits, 65537, NULL, NULL);
	int enlen=0,base64len=0;
	char* entag=encrypt("qic1",strlen("qic1"));
	entag=base64_encode(entag,encryptLEN,&base64len);

	strncpy(enUSER,entag,base64len);
	enUSER[base64len]='\0';
	printf("[L.B.test_qic1] %s (%d)\n",enUSER,strlen(enUSER));

	return 0;
}

int test_encrypt(char* rawData)
{
	int enlen=0,base64len=0;
	char* entag=encrypt(rawData,strlen(rawData));
	entag=base64_encode(entag,encryptLEN,&base64len);

	strncpy(enUSER,entag,base64len);
	enUSER[base64len]='\0';
	printf("[L.B.test] %s (%d)\n",enUSER,strlen(enUSER));

	return 0;
}

int test_decrypt(char* rawData)
{
	int delen=0,base64len=0;

	unsigned char entag[256]; //=base64_decode(rawData,strlen(rawData),&base64len);
	delen = rvDecodeB64(rawData, strlen(rawData), (unsigned char*)entag, 256);
	unsigned char* entag2=(unsigned char*)decrypt(entag,delen);
	strncpy(enUSER, entag2, delen/*decryptLEN*/);
	enUSER[delen/*decryptLEN*/]='\0';
	//printf("[L.B.test] %s (%d)\n",enUSER,strlen(enUSER));

	return 0;
}

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
jstring
Java_org_linphone_setup_SetupActivity_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
#if defined(__arm__)
  #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
   #define ABI "x86"
#elif defined(__x86_64__)
   #define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
   #define ABI "mips64"
#elif defined(__mips__)
   #define ABI "mips"
#elif defined(__aarch64__)
   #define ABI "arm64-v8a"
#else
   #define ABI "unknown"
#endif

	printf("[L.B.test] start openssl.\n");
	test_openssl();

    //return (*env)->NewStringUTF(env, "hello-openSSL2 from JNI !  Compiled with ABI " ABI ".");
    return (*env)->NewStringUTF(env, (const char*)enUSER);
}

jstring
Java_org_linphone_setup_SetupActivity_encryptFromJNI( JNIEnv* env,
                                                  jobject thiz, jstring rawData )
{
	const char *nativeString = (*env)->GetStringUTFChars(env, rawData, JNI_FALSE);
	test_encrypt((char*)nativeString);
	(*env)->ReleaseStringUTFChars(env, rawData, nativeString);
	return (*env)->NewStringUTF(env, (const char*)enUSER);
}

jstring
Java_org_linphone_setup_SetupActivity_decryptFromJNI( JNIEnv* env,
                                                  jobject thiz, jstring encryptedData )
{
	const char *nativeString = (*env)->GetStringUTFChars(env, encryptedData, JNI_FALSE);
	test_decrypt((char*)nativeString);
	return (*env)->NewStringUTF(env, (const char*)enUSER);
}

jint
Java_org_linphone_setup_SetupActivity_encryptLenFromJNI( JNIEnv* env,
                                                  jobject thiz, jstring rawData )
{
  return (jint)strlen(enUSER);
}

jint
Java_org_linphone_setup_SetupActivity_decryptLenFromJNI( JNIEnv* env,
                                                  jobject thiz, jstring rawData )
{
  return (jint)strlen(enUSER);
}
