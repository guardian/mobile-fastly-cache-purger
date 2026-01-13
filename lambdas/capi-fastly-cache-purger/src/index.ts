import { Callback, Context, Handler } from 'aws-lambda';

export const handler: Handler = async (
  event: unknown,
  context: Context,
  callback: Callback
): Promise<void> => {
  console.log('Hello World');
};
