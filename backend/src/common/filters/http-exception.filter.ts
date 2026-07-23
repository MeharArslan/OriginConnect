import { ExceptionFilter, Catch, ArgumentsHost, HttpException, HttpStatus, Logger } from '@nestjs/common';
@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private logger=new Logger('Filter');
  catch(exception:unknown,host:ArgumentsHost){
    const ctx=host.switchToHttp();const res=ctx.getResponse();const req=ctx.getRequest();
    let status=HttpStatus.INTERNAL_SERVER_ERROR;let message='Internal server error';
    if(exception instanceof HttpException){status=exception.getStatus();const r=exception.getResponse();message=typeof r==='string'?r:(r as any).message||message;}
    else this.logger.error(''+exception);
    res.status(status).json({statusCode:status,message,timestamp:new Date().toISOString(),path:req.url});
  }
}
