public void echo(Object[] args) {
	out.println("Running EchoArgs Groovy Script");
	int i = 0;
	if(args!=null && args.length > 0) {
		args.each() {
			out.println("Arg#${i}:${it}");
			i++;
		}
	} else {
		out.println("No Arguments Passed");
	}
}