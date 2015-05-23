public class Resource{
    private Position resourcePosition;

    protected Resource(int r, int c){
	resourcePosition = new Position(r,c);
    }

    public Position getPosition(){
	return resourcePosition;
    }
}
