
package linkedlistviewerplugin;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class LinkedListView extends ViewPart {

	public static final String ID = "linkedlistviewerplugin.LinkedListView";
	private TableViewer viewer;
	ViewContentProvider viewContentProvider;
	private Action doubleClickAction;

	public void SetLinkedList(LinkedListNode head)
	{
		this.viewContentProvider.SetLinkedList(head);
		viewer.refresh();
	}
	public LinkedListNode GetLinkedList()
	{
		return this.viewContentProvider.GetLinkedList();
	}
	class TableEntry
	{
		public TableEntry previous, next;
		public String name;
		public TableEntry(String name)
		{
			this.name = name;
			this.previous = this.next = null;
		}
		public void SetName(String name)
		{
			this.name = name;
		}
		public String GetName()
		{
			return this.name;
		}
		public String toString()
		{
			return this.name;
		}
	}	 
	class ViewContentProvider implements IStructuredContentProvider {
		TableEntry head = new TableEntry("NIL");
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			ArrayList<TableEntry> lst = new ArrayList<TableEntry>();
			TableEntry current = head;
			while(current != null)
			{
				lst.add(current);
				current = current.next;
			}
			return lst.toArray();
		}
		public LinkedListNode GetLinkedList()
		{
			LinkedListNode linkedListNode = GetLinkedListNode(head);
			return linkedListNode;
		}
		public void SetLinkedList(LinkedListNode node)
		{
			head = GetTableEntry(node);
		}
		TableEntry GetTableEntry(LinkedListNode node)
		{
			if(node == null)
			{
				return new TableEntry("NIL");
			}
			TableEntry current = new TableEntry(node.data);
			TableEntry nextEntry = GetTableEntry(node.next);
			nextEntry.previous = current;
			current.next = nextEntry;
			return current;
		}
		LinkedListNode GetLinkedListNode(TableEntry current)
		{
			if(current.GetName().equalsIgnoreCase("NIL"))
			{
				return null;
			}
			LinkedListNode node = new LinkedListNode();
			node.data = current.GetName();
			node.next = GetLinkedListNode(current.next);
			return node;
		}
	}	
	public LinkedListView() {
	}

	
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewContentProvider = new ViewContentProvider();
		viewer.setContentProvider(this.viewContentProvider);
		
		viewer.setInput(getViewSite());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "LinkedListViewerPlugin.viewer");
		makeActions();
		hookDoubleClickAction();
	}

	private void makeActions() {		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				TableEntry current  = (TableEntry)((IStructuredSelection)selection).getFirstElement();	
				InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), "", "Edit",current.GetName(), null);
				if(dialog.open() == Window.OK)
				{
					String newName = dialog.getValue();
					if(current.GetName().equalsIgnoreCase("NIL"))
					{
						TableEntry nextNode = new TableEntry("NIL");
						nextNode.previous = current;
						current.next = nextNode;
					}
					current.SetName(newName);
					viewer.refresh();
				}
			}
		};
	}
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}