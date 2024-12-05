FROM public.ecr.aws/lambda/java:11
COPY mobile-fastly-cache-purger.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["PurgerLambda::handleRequest"]
