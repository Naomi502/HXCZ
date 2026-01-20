// 云函数入口文件
const cloud = require('wx-server-sdk')
const WebSocket = require('ws')
const { v4: uuidv4 } = require('uuid')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const WSS_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4-EA85-44CA-94D1-A23F8E235091";
const VOICE = "zh-CN-XiaoxiaoNeural";

// 云函数入口函数
exports.main = async (event, context) => {
  const { text } = event;
  
  if (!text) {
    return { error: 'Text is required' };
  }

  return new Promise((resolve, reject) => {
    const audioChunks = [];
    const ws = new WebSocket(WSS_URL, {
      headers: {
        'Pragma': 'no-cache',
        'Cache-Control': 'no-cache',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0',
        'Origin': 'chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold',
        'Accept-Encoding': 'gzip, deflate, br, zstd',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6',
      }
    });

    ws.on('open', () => {
      console.log('Connected to Edge TTS');
      
      const configMsg = `X-Timestamp:${new Date().toString()}\r\nContent-Type:application/json; charset=utf-8\r\nPath:speech.config\r\n\r\n{"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}`;
      ws.send(configMsg);

      const requestId = uuidv4().replace(/-/g, '');
      const ssml = `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='zh-CN'><voice name='${VOICE}'><prosody rate='+0%' pitch='+0%'>${text}</prosody></voice></speak>`;
      const ssmlMsg = `X-RequestId:${requestId}\r\nContent-Type:application/ssml+xml\r\nX-Timestamp:${new Date().toString()}\r\nPath:ssml\r\n\r\n${ssml}`;
      ws.send(ssmlMsg);
    });

    ws.on('message', (data, isBinary) => {
      if (!isBinary) {
        const str = data.toString();
        if (str.includes('Path:turn.end')) {
          ws.close();
        }
      } else {
        // 解析二进制数据
        const view = new DataView(data.buffer, data.byteOffset, data.byteLength);
        const headerLen = view.getUint16(0, false);
        const headerBuffer = data.slice(2, 2 + headerLen);
        const headerStr = headerBuffer.toString();

        if (headerStr.includes('Path:audio')) {
          const audioData = data.slice(2 + headerLen);
          audioChunks.push(audioData);
        }
      }
    });

    ws.on('close', () => {
      // 合并音频
      const totalLen = audioChunks.reduce((acc, chunk) => acc + chunk.length, 0);
      const resultBuffer = Buffer.alloc(totalLen);
      let offset = 0;
      for (const chunk of audioChunks) {
        chunk.copy(resultBuffer, offset);
        offset += chunk.length;
      }
      
      // 返回 Base64
      resolve({
        audioBase64: resultBuffer.toString('base64')
      });
    });

    ws.on('error', (err) => {
      console.error('WebSocket Error:', err);
      reject({ error: err.message });
    });
    
    ws.on('unexpected-response', (request, response) => {
        console.error('Unexpected server response:', response.statusCode);
        reject({ error: `Unexpected server response: ${response.statusCode}` });
    });
  });
}
