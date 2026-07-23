import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
@Injectable()
export class NotificationsService {
  private readonly logger=new Logger('FCM');private app:any;
  constructor(private config:ConfigService){this.init();}
  private init(){try{const pid=this.config.get('FCM_PROJECT_ID');if(!pid){this.logger.warn('FCM not configured');return;}const admin=require('firebase-admin');if(!admin.apps.length)this.app=admin.initializeApp({credential:admin.credential.cert({projectId:pid,privateKey:this.config.get('FCM_PRIVATE_KEY')?.replace(/\\n/g,'\n'),clientEmail:this.config.get('FCM_CLIENT_EMAIL')})});else this.app=admin.apps[0];}catch(e){this.logger.error(e.message);}}
  async sendMessage(token:string,d:{senderId:string;senderName:string;senderPhoto?:string;conversationId:string;messageId:string;messageType:string;content?:string;}){
    if(!this.app||!token)return;
    try{await require('firebase-admin').messaging().send({token,notification:{title:d.senderName,body:d.content||('['+d.messageType+']'),imageUrl:d.senderPhoto},data:{type:'new_message',senderId:d.senderId,conversationId:d.conversationId,messageId:d.messageId},android:{priority:'high',notification:{channelId:'originconnect_messages',sound:'default'}}});}
    catch(e){this.logger.error(e.message);}
  }
}
