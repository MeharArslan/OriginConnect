import 'reflect-metadata';
import { NestFactory } from '@nestjs/core';
import { ValidationPipe, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppModule } from './app.module';
import { AllExceptionsFilter } from './common/filters/http-exception.filter';
import * as compression from 'compression';
import helmet from 'helmet';
async function bootstrap(){
  const logger=new Logger('Bootstrap');
  const app=await NestFactory.create(AppModule,{logger:['log','warn','error']});
  const config=app.get(ConfigService);const port=config.get<number>('PORT',3000);
  app.use(helmet());app.use(compression());
  app.enableCors({origin:'*',methods:['GET','POST','PUT','DELETE','PATCH'],allowedHeaders:['Content-Type','Authorization']});
  app.useGlobalPipes(new ValidationPipe({whitelist:true,transform:true,forbidNonWhitelisted:false}));
  app.useGlobalFilters(new AllExceptionsFilter());
  app.setGlobalPrefix('api/v1');
  await app.listen(port);
  logger.log('OriginConnect running on port '+port);
}
bootstrap();
